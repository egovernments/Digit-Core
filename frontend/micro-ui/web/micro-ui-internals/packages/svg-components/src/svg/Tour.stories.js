import React from "react";
import { Tour } from "./Tour";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Tour",
  component: Tour,
};

export const Default = () => <Tour />;
export const Fill = () => <Tour fill="blue" />;
export const Size = () => <Tour height="50" width="50" />;
export const CustomStyle = () => <Tour style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Tour className="custom-class" />;
export const Clickable = () => <Tour onClick={()=>console.log("clicked")} />;

const Template = (args) => <Tour {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};

