import React from "react";
import { AlarmAdd } from "./AlarmAdd";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "AlarmAdd",
  component: AlarmAdd,
};

export const Default = () => <AlarmAdd />;
export const Fill = () => <AlarmAdd fill="blue" />;
export const Size = () => <AlarmAdd height="50" width="50" />;
export const CustomStyle = () => <AlarmAdd style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AlarmAdd className="custom-class" />;
export const Clickable = () => <AlarmAdd onClick={()=>console.log("clicked")} />;

const Template = (args) => <AlarmAdd {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
