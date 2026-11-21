import React from "react";
import { SystemUpdateAlt } from "./SystemUpdateAlt";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SystemUpdateAlt",
  component: SystemUpdateAlt,
};

export const Default = () => <SystemUpdateAlt />;
export const Fill = () => <SystemUpdateAlt fill="blue" />;
export const Size = () => <SystemUpdateAlt height="50" width="50" />;
export const CustomStyle = () => <SystemUpdateAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SystemUpdateAlt className="custom-class" />;
export const Clickable = () => <SystemUpdateAlt onClick={()=>console.log("clicked")} />;

const Template = (args) => <SystemUpdateAlt {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
