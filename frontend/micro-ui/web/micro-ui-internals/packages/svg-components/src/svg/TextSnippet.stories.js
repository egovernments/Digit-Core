import React from "react";
import { TextSnippet } from "./TextSnippet";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "TextSnippet",
  component: TextSnippet,
};

export const Default = () => <TextSnippet />;
export const Fill = () => <TextSnippet fill="blue" />;
export const Size = () => <TextSnippet height="50" width="50" />;
export const CustomStyle = () => <TextSnippet style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TextSnippet className="custom-class" />;
export const Clickable = () => <TextSnippet onClick={()=>console.log("clicked")} />;

const Template = (args) => <TextSnippet {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
