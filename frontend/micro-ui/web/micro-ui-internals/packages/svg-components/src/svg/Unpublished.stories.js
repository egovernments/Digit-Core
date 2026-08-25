import React from "react";
import { Unpublished } from "./Unpublished";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Unpublished",
  component: Unpublished,
};

export const Default = () => <Unpublished />;
export const Fill = () => <Unpublished fill="blue" />;
export const Size = () => <Unpublished height="50" width="50" />;
export const CustomStyle = () => <Unpublished style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Unpublished className="custom-class" />;
export const Clickable = () => <Unpublished onClick={()=>console.log("clicked")} />;

const Template = (args) => <Unpublished {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
